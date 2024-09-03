"""
this is a simple module to read and convert XML data to a Python dictionary.

"""
from datetime import datetime, timedelta, time
import xml.etree.ElementTree as Etree
import requests
from .logger import get_logger


def xml_to_dict(url: str) -> dict:
    """
    Loads the XML data from the specify host URL and convert to a dictionary.

    :param url: The URL of the XML data.
    :return: The XML data as a dictionary.
    """
    logger = get_logger(__name__)

    try:
        # Make a GET request to the URL and retrieve the XML content
        response = requests.get(url, timeout=10)
        xml_content = response.content

        if not xml_content:
            raise requests.exceptions.RequestException("No XML Data returned")

        # Parse the XML content
        root = Etree.fromstring(xml_content)

        # Convert the XML tree to a Python dictionary
        xml_dict = xml_to_dict_recursive(root)

        return xml_dict

    except requests.exceptions.Timeout:
        logger.critical("The request timed out.")
    except requests.exceptions.RequestException as e:
        logger.critical("An error occurred: %s", str(e))

    return {'ProjectName': 'Unknown', 'Areas': None}


def xml_to_dict_recursive(element):  # NOSONAR: python:S3776
    """
    Expects an Etree element and converts it to a Python dictionary.

    :param element: The ETree element to convert.
    :return: A Python dictionary representation of the ETree element.
    """
    logger = get_logger(__name__)

    result = {}

    # Process attributes
    for key, value in element.attrib.items():
        logger.debug("Processing attribute: %s = %s", key, value)

        converted_text = convert_text(value)
        result[key] = converted_text

    # Process text content
    if not result and not element.findall(".//*"):
        text = element.text
        if text is not None:
            converted_text = convert_text(text)
            if converted_text is not None:
                return converted_text

    # Process children elements
    for child in element:
        child_dict = xml_to_dict_recursive(child)
        if child.tag in result:
            if isinstance(result[child.tag], list):
                result[child.tag].append(child_dict)
            else:
                result[child.tag] = [result[child.tag], child_dict]
        else:
            result[child.tag] = child_dict

    return result


def convert_text1(text):  # noqa: C901
    """
    Converts the 'text' from an Etree element to a python primitive type.
    It will try to convert the text to an int, float, datetime, time, or timedelta.

    :param text: The text to convert.
    :return: an integer, float, datetime, time, or timedelta representation
    """
    if not text:
        return None

    # Convert to int
    try:
        return int(text)
    except ValueError:
        pass

    # Convert to float
    try:
        return float(text)
    except ValueError:
        pass

    # Convert to datetime if in 'MM/DD/YYYY' format
    try:
        return datetime.strptime(text, '%m/%d/%Y').date()
    except ValueError:
        pass

    # Convert to time if in 'HH:MM:SS' format with fractional seconds
    try:
        time_parts = text.split(':')
        hours = int(time_parts[0])
        minutes = int(time_parts[1])
        if '.' in time_parts[2]:
            seconds = float(time_parts[2])
            return timedelta(hours=hours, minutes=minutes, seconds=seconds)
        seconds = int(time_parts[2])
        return time(hours, minutes, int(seconds))
    except (ValueError, IndexError):
        pass

    # if everything fails, then just return the original text
    return text.strip()

def convert_text(text):
    """
    Converts the 'text' from an Etree element to a python primitive type.
    It will try to convert the text to an int, float, datetime, time, or timedelta.

    :param text: The text to convert.
    :return: an integer, float, datetime, time, or timedelta representation
    """
    if not text:
        return None

    converters = [
        int,
        float,
        lambda x: datetime.strptime(x, '%m/%d/%Y').date(),
        lambda x: timedelta(
            hours=int(x.split(':')[0]),
            minutes=int(x.split(':')[1]),
            seconds=float(x.split(':')[2])
        ) if '.' in x.split(':')[2] else time(
            int(x.split(':')[0]),
            int(x.split(':')[1]),
            int(x.split(':')[2])
        )
    ]

    for convert in converters:
        try:
            return convert(text)
        except (ValueError, IndexError):
            pass

    return text.strip()
