"""
This is a prometheus exporter for the Lutron QS processor.

It will login to the Lutron host and retrieve the configuration data as well as all zone levels for
each area defined in the processor configuration.  It will also retrieve the keypad LED status for each button.

"""
import json
import os
from typing import Optional
from .logger import get_logger


logger = get_logger(__name__)


class ConfigurationData:
    """ manage the configuration data for the target host """
    _instance: Optional[dict] = None

    @classmethod
    def get_instance(cls) -> dict:
        """
        Return the JSON configuration for the target host as a dictionary.

        :return: Configuration dictionary.
        """
        if cls._instance is None:
            cls._instance = cls._load_config()
            address = cls._instance.get('address', None)
            if address:
                logger.warning('Configuration loaded for %s', address)
                logger.debug(cls._instance)
        return cls._instance

    @staticmethod
    def _load_config() -> dict:
        """ Load the configuration file """
        try:
            if not os.path.exists('config.json'):
                raise OSError('config.json not found')
            with open('config.json', 'r', encoding='utf-8') as f:
                return json.load(f)
        except OSError as e:
            logger.critical("Configuration error: %s", str(e))
        return {}


def get_host_data() -> dict:
    """ Return the json configuration for the target host into a dictionary. """

    return ConfigurationData.get_instance()
