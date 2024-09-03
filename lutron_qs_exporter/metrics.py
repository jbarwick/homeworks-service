"""
This module provides a Prometheus Exporter for Lutron QS processors.

It reads the Lutron QS configuration database by calling the URL of your QS processor which
provides an XML representation of all of your Areas, Zones, and Devices.

It then reads each of the Device levels using Telnet to connect to the QS processor and
responds to the HTTP request "/metrics" to provide Prometheus metrics for your light levels.
"""
import re
import telnetlib
import time
from datetime import datetime
from typing import Tuple

from prometheus_client import Gauge, Info

from .database import xml_to_dict
from .config import get_host_data
from .logger import get_logger

TIMEOUT = 2
ACTION_PROMPT = b"QNET?"
COMMAND_PROMPT = b"QNET>"

information_panel = Info('lutron', 'Information about the processor')

lutron_zone_level = Gauge(
    'lutron_zone_level', 'Lutron Zone Level',
    ['area', 'area_id', 'zone', 'zone_name']
)

lutron_zone_watts = Gauge(
    'lutron_zone_watts', 'Wattage for the current zone',
    ['area', 'area_id', 'zone', 'zone_name', 'total_watts']
)

lutron_device_leds = Gauge(
    'lutron_device_led', 'State of the LED',
    ['area', 'area_id', 'device_id', 'device_name', 'led_id', 'led_name']
)


class DesignData:
    """ Manage the design data singletons """
    _design_data: dict = {}
    _design_version: int = 0

    @classmethod
    def get_design_data(cls, dv: int = 0) -> dict:
        """ return the design data, or laod it """
        logger = get_logger("design")

        if not dv:
            return cls._design_data

        if not cls._design_data or dv != cls._design_version:
            logger.warning("Design version %s has changed. Reloading...", dv)
            cls._design_data = cls._load_design_data(dv)
            cls._design_version = dv if cls._design_data else 0
        else:
            logger.warning("Design version %s has not changed.", dv)

        logger.debug(cls._design_data)
        return cls._design_data

    @classmethod
    def get_design_version(cls) -> int:
        """ return the version last loaded """
        return cls._design_version

    @classmethod
    def _load_design_data(cls, dv) -> dict:
        """ load the data from the processor """
        logger = get_logger("design")

        host_data = get_host_data()
        if host_data:
            address = host_data.get('address')
            if address:
                url = "http://" + address + "/DbXmlInfo.xml"
                logger.debug("Reading from %s", url)
                design_data = xml_to_dict("http://" + address + "/DbXmlInfo.xml")
                project_details = design_data.get('ProjectName', 'Unknown')
                if project_details != 'Unknown':
                    name = project_details['ProjectName']
                    logger.warning("Design reloaded for %s. Design version: %s", name, dv)
                    return design_data
                logger.error("Cannot retrieve design from processor")
            else:
                logger.error('No address information found in the configuration file')
        else:
            logger.error("No configuration file could be loaded!")
        return {}


def get_design_data() -> dict:
    """ return the currently loaded design data"""
    return DesignData.get_design_data()


def get_design_version() -> int:
    """ return the design version number of the QS processor.datbase """
    return DesignData.get_design_version()


def login():
    """
    Login to the QS processor.  This is done for each call to refresh_metrics.

    :return:
    """
    host_data = get_host_data()

    address = host_data.get('address')
    username = host_data.get('username')

    login_logger = get_logger("login")
    login_logger.warning("Logging in to %s as %s...", address, username)

    address = host_data.get('address')
    port = host_data.get('port')

    tn = telnetlib.Telnet(address, port)
    tn.read_until(b'login: ', TIMEOUT)

    tn.write(f"{username}\r\n".encode('ascii'))
    tn.read_until(b'password: ', TIMEOUT)

    password = host_data.get('password')
    tn.write(f"{password}\r\n".encode('ascii'))
    tn.read_until(ACTION_PROMPT, TIMEOUT)

    return tn


def parse_date(text: str) -> Tuple[str, str]:
    """
    Parse the DATETIME and FIRMWARE revision from the QS processor. A tuple is returned with both values.

    :param text: The input text containing the firmware revision and date.
    :return: A tuple containing the firmware version and the parsed date as a string.
    """
    match = re.search(r"OS Firmware Revision = ([\d.]+) \[([^[\]]+)]", text)
    if match:
        fv = match.group(1)
        fds = match.group(2)
        # Convert date string to datetime object and then to string
        fd = datetime.strptime(fds, "%b %d %Y %H:%M:%S").isoformat()
    else:
        fv = '0'
        fd = datetime.now().isoformat()

    return fv, fd


def parse_version(line: str) -> int:
    """
    Parse the VERSION line from the QS processor and return the firmware version.

    :param line:
    :return:
    """
    version_logger = get_logger("version")
    version_logger.debug(line)

    match = re.search(r'= (\d+)', line.strip()) if line else None
    return int(match.group(1)) if match else 0


def parse_revision(line: str) -> str:
    """
    Parse the REVISION line from the QS processor and return the firmware version.

    :param line: The input line containing the firmware revision.
    :return: The parsed firmware revision as a string.
    """
    revision_logger = get_logger("revision")
    revision_logger.debug(line)

    match = re.search(r'= (.+)', line) if line else None
    return match.group(1).strip() if match else ''


def parse_output(line: str) -> float:
    """
    Parse the OUTPUT line from the QS processor and return the zone level.

    :param line: The input line containing the OUTPUT information.
    :return: The parsed zone level as a float.
    """

    output_logger = get_logger("output")
    output_logger.debug(line)

    if line:
        elements = line.split(',')
        if elements[0].strip() == "~OUTPUT" and len(elements) > 3:
            try:
                return float(elements[3])
            except ValueError:
                output_logger.error("Unable to parse zone level: %s", elements[3])

    return 0.0


def parse_device(line: str) -> float:
    """
    Get the Device information LED status.

    :param line: The input line containing the DEVICE information.
    :return: The parsed LED status as a float.
    """
    device_logger = get_logger("device")
    device_logger.debug(line)

    if line:
        elements = line.split(',')
        if elements[0].strip() == '~DEVICE' and len(elements) > 4:
            try:
                return float(elements[4])
            except ValueError:
                device_logger.error("Unable to parse LED state: %s", elements[4])

    return 0.0


def populate_info(tn) -> dict:
    """
    Populates the information panel with information about the processor.

    :param tn:
    :return:
    """
    # pylint: disable=global-statement

    info_logger = get_logger("info")

    tn.write(b'FIRMWAREREV\r\n')

    firmware_version, firmware_date = parse_date(tn.read_until(b'\r\n').decode('ascii').strip())
    dv: int = parse_version(tn.read_until(b'\r\n').decode('ascii'))
    firmware_revision: str = parse_revision(tn.read_until(b'\r\n').decode('ascii').strip())

    tn.read_until(COMMAND_PROMPT)

    info = {
        'processor_type': 'Lutron QS Processor',
        'firmware_version': str(firmware_version).strip(),
        'firmware_date': str(firmware_date).strip(),
        'firmware_revision': str(firmware_revision).strip(),
        'design_version': str(dv),
    }

    info_logger.warning("QS Processor v%s, %s, rev %s. Design version %s",
                        firmware_version, firmware_date, firmware_revision, dv)
    info_logger.debug(info)

    information_panel.info(info)

    return DesignData.get_design_data(dv)


def populate_component(tn, button_cache, area_id, area_name, device, component):
    """
    Populote Button and LED levels data for Prometheus metrics data

    :param tn:
    :param button_cache:
    :param area_id:
    :param area_name:
    :param device:
    :param component:
    :return:
    """
    component_logger = get_logger("component")
    component_logger.debug(component)

    device_id = device['IntegrationID']
    device_name = device['Name']

    component_type = component['ComponentType'].upper()
    component_id = component['ComponentNumber']

    if component_type == 'BUTTON':

        button_logger = get_logger("button")

        button_data = component['Button']
        button_logger.debug(button_data)

        button_name = button_data.get('Engraving', '')
        button_logger.info("Found button %s: %s", component_id, button_name)
        button_cache.append(button_data)

    elif component_type == 'LED':

        led_logger = get_logger("led")

        button_data = button_cache.pop(0)
        led_logger.debug(button_data)

        led_name = button_data.get('Engraving', '')

        tn.write(f'?DEVICE,{device_id},{component_id},9\r\n'.encode('ascii'))
        led_state = parse_device(tn.read_until(b'\r\n').decode('ascii').strip())
        tn.read_until(COMMAND_PROMPT)

        onoff = 'ON' if int(led_state) else 'OFF'
        led_logger.info("Reading LED %s - %s: %s - %s", component_id, led_name, onoff, led_state)

        lutron_device_leds.labels(
            area=area_name, area_id=area_id, device_id=device_id,
            device_name=device_name, led_id=component_id, led_name=led_name).set(led_state)


def populate_device(tn, area_id, area_name, device):
    """
    Populate the device metrics data for Prometheus metrics data

    :param tn:
    :param area_id:
    :param area_name:
    :param device:
    :return:
    """
    device_logger = get_logger("device")
    device_logger.debug(device)

    device_id = device['IntegrationID']
    device_name = device['Name']
    serial_number = device['SerialNumber']
    device_logger.info("Reading device %s...Area %s - %s/%s. SN: %s",
                       device_id, area_id, area_name, device_name, serial_number)

    button_cache = []

    components = device['Components'].get('Component', [])
    if not isinstance(components, list):
        components = [components]

    device_logger.debug("%s, Found %d components", area_name, len(components))
    for component in components:
        populate_component(tn, button_cache, area_id, area_name, device, component)


def populate_group(tn, area_id, area_name, group):
    """
    Populate group metrics data for Prometheus metrics data.

    :param tn:
    :param area_id:
    :param area_name:
    :param group:
    :return:
    """
    group_logger = get_logger("group")
    group_logger.debug(group)

    group_name = group['Name']
    group_logger.info("Reading device group %s...Area %s - %s", group_name, area_id, area_name)

    devices = group['Devices'].get('Device', [])
    if not isinstance(devices, list):
        devices = [devices]

    ld = len(devices)
    group_logger.debug("%s, Found %d device%s", area_name, ld, '' if ld == 1 else 's')
    for device in devices:
        populate_device(tn, area_id, area_name, device)


def populate_zone(tn, area_id, area_name, zone_data):
    """
    Populate zone metrics data for Prometheus metrics data.

    :param tn:
    :param area_id:
    :param area_name:
    :param zone_data:
    :return:
    """
    zone_logger = get_logger("zone")
    zone_logger.debug(zone_data)

    zone_id = zone_data['IntegrationID']
    zone_name = zone_data['Name']

    zone_total_watts = float(zone_data['Wattage'])

    # Execute command and retrieve output
    tn.write(f'?OUTPUT,{zone_id},1\r\n'.encode('ascii'))
    zone_level = parse_output(tn.read_until(b'\r\n').decode('ascii').strip())
    tn.read_until(COMMAND_PROMPT)

    zone_watts = round(zone_total_watts * zone_level / 100.0, 1)

    zone_logger.info("Populating zone %s...Area %s - %s/%s: %s Watts at %s%%",
                     zone_id, area_id, area_name, zone_name, zone_watts, zone_level)

    lutron_zone_level.labels(
        area=area_name, area_id=str(area_id), zone=str(zone_id), zone_name=zone_name).set(zone_level)
    lutron_zone_watts.labels(
        area=area_name, area_id=str(area_id), zone=str(zone_id), zone_name=zone_name,
        total_watts=str(zone_total_watts)).set(zone_watts)


def populate_area(tn, area):
    """
    Iterate through the specified area and find all zone and group details and populate metrics data.

    :param tn:
    :param area:
    :return:
    """
    area_logger = get_logger("area")
    area_logger.debug(area)

    area_id = area['IntegrationID']
    area_name = area['Name']

    area_logger.info("Populating area %s - %s...", area_id, area_name)

    zones = area['Outputs'].get('Output', [])
    if not isinstance(zones, list):
        zones = [zones]

    ld = len(zones)
    area_logger.info('%s: %s zone%s', area_name, ld, '' if ld == 1 else 's')
    for zone_data in zones:
        populate_zone(tn, area_id, area_name, zone_data)

    groups = area['DeviceGroups'].get('DeviceGroup', [])
    if not isinstance(groups, list):
        groups = [groups]

    ld = len(groups)
    area_logger.info('%s: %s group%s', area_name, ld, '' if ld == 1 else 's')
    for group in groups:
        populate_group(tn, area_id, area_name, group)


def populate_floor(tn, floor):
    """
    For the specified floor and find all area details and populate metrics data.

    :param tn:
    :param floor:
    :return:
    """
    floor_logger = get_logger("floor")
    floor_logger.debug(floor)

    floor_id = floor['IntegrationID']
    floor_name = floor['Name']

    floor_logger.info("Populating floor %s %s...", floor_id, floor_name)

    areas = floor['Areas'].get('Area', [])
    if not isinstance(areas, list):
        areas = [areas]

    floor_logger.info('Refreshing %s area(s)', len(areas))
    for area in areas:
        populate_area(tn, area)


def parse_time_of_day(line: str):
    """
    Parse the time of day from the output of the command.

    :param line:
    :return:
    """
    elements = line.split(',')
    v = elements[3] if elements[0].strip() == '~SYSVAR' else '0'
    return 'Day' if v else 'Night'


def populate_time_of_day(tn):
    """
    Get the time of day from the Lutron host and store it in the Prometheus information panel.

    :param tn:
    :return:
    """
    tn.write(b'?SYSVAR,23,1\r\n')
    result = parse_time_of_day(tn.read_until(b'\r\n').decode('ascii'))
    information_panel.info({'time_of_day': result})


async def refresh_metrics():
    """
    A function that refreshes the Prometheus metrics data.  Find floors, areas, zones, devices, and groups
    by querying the Lutron host for the current design and iterating through the design elements.

    :return:
    """
    metrics_logger = get_logger("metrics")
    metrics_logger.warning("Refreshing metrics - Design Version: %s...", get_design_version())

    host_data = get_host_data()

    if not host_data:
        metrics_logger.error(
            "Error refreshing metrics.  There is no host data information.  Check your configuration file!")
        return False

    start_time = time.time()

    tn = login()

    try:
        design = populate_info(tn)
        floors = design['Areas'].get('Area', [])
        floors = [floors] if not isinstance(floors, list) else floors
        metrics_logger.info('Refreshing %s floor(s)', len(floors))
        for floor in floors:
            populate_floor(tn, floor)
    finally:
        tn.close()

    end_time = time.time()
    elapsed_time = end_time - start_time

    # Format the elapsed time as seconds and milliseconds
    elapsed_seconds = int(elapsed_time)
    elapsed_milliseconds = int((elapsed_time - elapsed_seconds) * 1000)

    metrics_logger.warning('Metrics refreshed. %d seconds, %d milliseconds', elapsed_seconds, elapsed_milliseconds)

    return True
