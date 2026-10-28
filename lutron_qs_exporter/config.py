"""
Load Lutron QS connection settings from the process environment.

Compose sets these variables. There is no config file.
"""

import os
from typing import Optional

from .logger import get_logger


class ConfigurationData:
    """Process-wide copy of the processor connection settings."""

    _instance: Optional[dict] = None

    @classmethod
    def get_instance(cls) -> dict:
        """
        Return the processor connection settings.

        Keys are address, port, username, password, and log_level.
        An empty dict means required variables are missing.
        """
        if cls._instance is None:
            cls._instance = cls._load_config()
        return cls._instance

    @staticmethod
    def _load_config() -> dict:
        """Read LUTRON_* and LOG_LEVEL from the environment."""
        logger = get_logger(__name__, os.getenv("LOG_LEVEL", "INFO"))

        address = os.getenv("LUTRON_ADDRESS", "").strip()
        username = os.getenv("LUTRON_USERNAME", "").strip()
        password = os.getenv("LUTRON_PASSWORD", "")
        port_raw = os.getenv("LUTRON_PORT", "23").strip() or "23"
        log_level = os.getenv("LOG_LEVEL", "INFO").strip() or "INFO"

        missing = [
            name
            for name, value in (
                ("LUTRON_ADDRESS", address),
                ("LUTRON_USERNAME", username),
                ("LUTRON_PASSWORD", password),
            )
            if value == ""
        ]
        if missing:
            logger.critical(
                "Missing required environment variables: %s", ", ".join(missing)
            )
            return {}

        try:
            port = int(port_raw)
        except ValueError:
            logger.critical("LUTRON_PORT must be an integer, got %s", port_raw)
            return {}

        logger.warning("Configuration loaded for %s:%s", address, port)
        return {
            "address": address,
            "port": port,
            "username": username,
            "password": password,
            "log_level": log_level,
        }


def get_host_data() -> dict:
    """Return the processor connection settings."""

    return ConfigurationData.get_instance()
