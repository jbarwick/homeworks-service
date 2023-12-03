"""
This is a prometheus exporter for the Lutron QS processor.

It will login to the Lutron host and retrieve the configuration data as well as all zone levels for
each area defined in the processor configuration.  It will also retrieve the keypad LED status for each button.

"""
import json
import os
from typing import Optional

from .logger import get_logger

host_data: Optional[dict] = None


def get_host_data() -> Optional[dict]:
    """
    Return the json configuration for the target host into a dictionary.

    :return:
    """
    # pylint: disable=global-statement
    global host_data

    if host_data:
        return host_data

    try:

        if os.path.exists('config.json'):
            with open('config.json', 'r', encoding='utf-8') as f:
                host_data = json.load(f)
        else:
            raise FileNotFoundError('config.json not found')

    except FileNotFoundError:
        host_data = {'log_level': 'INFO', 'error': 'The config.json file could not be found. '
                                                   'Please create the system config file.'}
    except OSError as e:
        host_data = {'log_level': 'INFO', 'error': str(e)}

    logger = get_logger(__name__, host_data.get('log_level', os.getenv('LOG_LEVEL', 'info')))

    if 'error' in host_data:
        logger.critical("Configuration error: %s", host_data.get('error'))
    else:
        host = host_data.get('address', '')
        logger.info('Configuration loaded for %s', host)
        logger.debug(host_data)

    return host_data
