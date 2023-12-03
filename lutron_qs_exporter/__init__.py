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

from fastapi import FastAPI
from prometheus_client import Gauge, Info, generate_latest, CollectorRegistry
from starlette.responses import Response, RedirectResponse

from .database import xml_to_dict
from .config import get_host_data
from .logger import get_logger

app = FastAPI()

TIMEOUT = 2
ACTION_PROMPT = b"QNET?"
COMMAND_PROMPT = b"QNET>"

host_data = get_host_data()
logger = get_logger(__name__, host_data.get('log_level'))


def print_version():
    """
    Print the version of the exporter to te logger.

    :return:
    """
    try:
        with open('version.txt', 'r', encoding='utf-8') as f:
            version = f.read().strip()
    except FileNotFoundError:
        version = "0.0.0"

    logger.info("Lutron QS Exporter Version: %s", version)


print_version()

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

design_data: dict = {}
design_version: int = 0


def get_app():
    """
    Return the FastAPI application.

    :return:
    """
    return app


def login():
    """
    Login to the QS processor.  This is done for each call to refresh_metrics.

    :return:
    """
    logger.debug("Logging in...")

    tn = telnetlib.Telnet(host_data['address'], host_data['port'])
    tn.read_until(b'login: ', TIMEOUT)
    tn.write(f"{host_data['username']}\r\n".encode('ascii'))
    tn.read_until(b'password: ', TIMEOUT)
    tn.write(f"{host_data['password']}\r\n".encode('ascii'))
    tn.read_until(ACTION_PROMPT, TIMEOUT)

    return tn


def parse_date(text: str) -> (str, str):
    """
    Parse the DATETIME and FIRMWARE revision from the QS processor.  A tuple is returned with both values.

    :param text:
    :return:
    """
    match = re.search(r"OS Firmware Revision = ([\d.]+) \[([^[\]]+)]", text)
    if match:
        fv = match.group(1)
        fds = match.group(2)

        # convert date string to datetime object
        fd = datetime.strptime(fds, "%b %d %Y %H:%M:%S")
    else:
        fv = '0'
        fd = datetime.now()

    return fv, fd


def parse_version(line: str) -> int:
    """
    Parse the VERSION line from the QS processor and return the firmware version.

    :param line:
    :return:
    """
    logger.debug("Parsing version...")
    logger.debug(line)
    if line:
        match = re.search(r'= (.+)', line.strip())
        return int(match.group(1).strip() if match else 0)
    return 0


def parse_revision(line: str) -> str:
    """
    Parse the REVISION line from the QS processor and return the firmware version.

    :param line:
    :return:
    """
    logger.debug("Parsing revision...")
    logger.debug(line)
    if line:
        match = re.search(r'= (.+)', line)
        return match.group(1).strip() if match else ''
    return ''


def parse_output(line: str) -> float:
    """
    Parse the OUTPUT line from the QS processor and return the zone level.

    :param line:
    :return:
    """
    logger.debug("Parsing output...")
    logger.debug(line)

    zone_level = 0.0
    if line:
        elements = line.split(',')
        if elements[0].strip() == "~OUTPUT":
            zone_level = float(elements[3])

    return zone_level


def parse_device(line: str):
    """
    Get the Device information LED status
    :param line:
    :return:
    """
    logger.debug("Parsing device...")
    logger.debug(line)
    led_state = 0.0

    if line:
        elements = line.split(',')
        if elements[0].strip() == '~DEVICE':
            led_state = float(elements[4])

    return led_state


def populate_info(tn) -> dict:
    """
    Populates the information panel with information about the processor.

    :param tn:
    :return:
    """
    # pylint: disable=global-statement
    global design_version, design_data

    tn.write(b'FIRMWAREREV\r\n')
    firmware_version, firmware_date = parse_date(tn.read_until(b'\r\n').decode('ascii'))
    dv: int = parse_version(tn.read_until(b'\r\n').decode('ascii'))
    firmware_revision: str = parse_revision(tn.read_until(b'\r\n').decode('ascii'))
    tn.read_until(COMMAND_PROMPT)

    info = {
        'processor_type': 'Lutron QS Processor',
        'firmware_version': str(firmware_version).strip(),
        'firmware_date': str(firmware_date).strip(),
        'firmware_revision': str(firmware_revision).strip(),
        'design_version': str(dv),
    }

    logger.debug("Populating info...")
    logger.debug(info)

    information_panel.info(info)

    # Lod the design from the processor if it has changed
    if dv != design_version:
        design_data = xml_to_dict(f"http://{host_data['address']}/DbXmlInfo.xml")
        design_version = dv

        if 'ProjectName' in design_data:
            project_name = design_data['ProjectName'].get('ProjectName')
        else:
            project_name = 'Unknown'

        logger.warning("Design reloaded for %s. Version: %s", project_name, dv)

    return design_data if design_data else {'Areas': None}


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
    device_id = device['IntegrationID']
    device_name = device['Name']

    component_type = component['ComponentType'].upper()

    if component_type == 'BUTTON':

        logger.debug("Populating button...")
        logger.debug(component)

        button_cache.append(component['Button'])

    elif component_type == 'LED':

        logger.debug("Populating keypad LED...")
        logger.debug(component)

        button = button_cache.pop(0)

        led_id = component['ComponentNumber']
        led_name = button.get('Engraving', '')

        tn.write(f'?DEVICE,{device_id},{led_id},9\r\n'.encode('ascii'))
        led_state = parse_device(tn.read_until(b'\r\n').decode('ascii'))
        tn.read_until(COMMAND_PROMPT)

        lutron_device_leds.labels(area=area_name, area_id=area_id, device_id=device_id,
                                  device_name=device_name, led_id=led_id, led_name=led_name) \
            .set(led_state)


def populate_device(tn, area_id, area_name, device):
    """
    Populate the device metrics data for Prometheus metrics data

    :param tn:
    :param area_id:
    :param area_name:
    :param device:
    :return:
    """
    logger.debug("Populating device...Area %s - %s", area_id, area_name)
    logger.debug(device)

    button_cache = []

    components = device['Components'].get('Component')
    if components:
        if not isinstance(components, list):
            components = [components]
        logger.debug("%s, Found %d components", area_name, len(components))
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
    logger.debug("Populating group...Area %s - %s", area_id, area_name)
    logger.debug(group)

    devices = group['Devices'].get('Device')
    if devices:
        if not isinstance(devices, list):
            devices = [devices]
        logger.debug("%s, Found %d devices", area_name, len(devices))
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
    logger.debug("Populating zone...Area %s - %s", area_id, area_name)
    logger.debug(zone_data)

    zone_id = zone_data['IntegrationID']
    zone_name = zone_data['Name']
    zone_total_watts = float(zone_data['Wattage'])

    # Execute command and retrieve output
    tn.write(f'?OUTPUT,{zone_id},1\r\n'.encode('ascii'))
    zone_level = parse_output(tn.read_until(b'\r\n').decode('ascii'))
    tn.read_until(COMMAND_PROMPT)

    zone_watts = round(zone_total_watts * zone_level / 100.0, 1)

    lutron_zone_level.labels(area=area_name, area_id=str(area_id), zone=str(zone_id), zone_name=zone_name) \
        .set(zone_level)
    lutron_zone_watts.labels(area=area_name, area_id=str(area_id), zone=str(zone_id), zone_name=zone_name,
                             total_watts=str(zone_total_watts)) \
        .set(zone_watts)


def populate_area(tn, area):
    """
    Iterate through the specified area and find all zone and group details and populate metrics data.

    :param tn:
    :param area:
    :return:
    """
    logger.debug("Populating area...")
    logger.debug(area)

    area_id = area['IntegrationID']
    area_name = area['Name']

    logger.info('Refreshing area %s', area_name)

    zones = area['Outputs'].get('Output')
    if zones:
        if not isinstance(zones, list):
            zones = [zones]
        logger.info('%s: %s zone(s)', area_name, len(zones))
        for zone_data in zones:
            populate_zone(tn, area_id, area_name, zone_data)

    groups = area['DeviceGroups'].get('DeviceGroup')
    if groups:
        if not isinstance(groups, list):
            groups = [groups]
        logger.info('%s: %s group(s)', area_name, len(groups))
        for group in groups:
            populate_group(tn, area_id, area_name, group)


def populate_floor(tn, floor):
    """
    For the specified floor and find all area details and populate metrics data.

    :param tn:
    :param floor:
    :return:
    """
    logger.debug("Populating floor...")
    logger.debug(floor)

    areas = floor['Areas'].get('Area')
    if areas:
        if not isinstance(areas, list):
            areas = [areas]
        logger.info('Refreshing %s area(s)', len(areas))
        for area in areas:
            populate_area(tn, area)


def parse_time_of_day(line: str):
    """
    Parse the time of day from the output of the command.

    :param line:
    :return:
    """
    elements = line.split(',')
    if elements[0].strip() == '~SYSVAR':
        v = elements[3]
    else:
        v = 0
    return 'Night' if not v else 'Day'


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
    if 'error' in host_data:
        logger.error("Error refreshing metrics: %s", host_data['error'])
        return False

    start_time = time.time()
    logger.info("Refreshing metrics...")

    tn = login()

    try:
        design = populate_info(tn)
        floors = design['Areas'].get('Area')
        if floors:
            if not isinstance(floors, list):
                floors = [floors]
            logger.info('Refreshing %s floor(s)', len(floors))
            for floor in floors:
                populate_floor(tn, floor)
    finally:
        tn.close()

    end_time = time.time()
    elapsed_time = end_time - start_time

    # Format the elapsed time as seconds and milliseconds
    elapsed_seconds = int(elapsed_time)
    elapsed_milliseconds = int((elapsed_time - elapsed_seconds) * 1000)

    logger.info('Metrics refreshed. %d seconds, %d milliseconds', elapsed_seconds, elapsed_milliseconds)

    return True


@app.get("/", include_in_schema=False)
async def root():
    """
    Redirect calls the root path to the /metrics endpoint.
    :return:
    """
    return RedirectResponse(url="/metrics", status_code=302)


@app.get("/metrics")
async def metrics():
    """
    The primary endpoint for the metrics data.  Prometheous will call the '/metrics' endpoint to
    retrieve the metrics data.
    :return:
    """
    await refresh_metrics()

    registry = CollectorRegistry()

    registry.register(information_panel)
    registry.register(lutron_zone_level)
    registry.register(lutron_zone_watts)
    registry.register(lutron_device_leds)

    metrics_data = generate_latest(registry)

    logger.debug("Generated metrics...")
    logger.debug(metrics_data.decode('utf-8'))

    return Response(content=metrics_data, media_type="text/plain")


@app.get("/data")
def get_data():
    """
    This is a little helper function to simply return the design data that was retrieved from the Lutron host.

    :return:
    """
    return design_data


@app.get('/config')
def get_config():
    """
    A simple endpoint to return the host configuration data.

    :return:
    """
    return host_data
