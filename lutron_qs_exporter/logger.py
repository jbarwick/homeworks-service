"""
Initialize a logger for my application.

This custom logger is opinionated about the format.  You can't change it but you can re-code it if you want.

The feature here is that if you pass a dict to the logger, it will convert it to a JSON string and output to the log.

Also, the date is output in the format: YYYY-MM-DD HH:MM:SS.

"""
import json
import logging
import os
from datetime import datetime, date, time, timedelta
from decimal import Decimal
from typing import Optional


class CustomFormatter(logging.Formatter):
    """
    Create a custom formatter for the logger. This formatter will output the date and time in
    the format: YYYY-MM-DD HH:MM:SS.
    It will also convert the log message into JSON if the message is a dictionary.
    """

    def __init__(self, datefmt='%Y-%m-%d %H:%M:%S', **kwargs):
        super().__init__(**kwargs)
        self.datefmt = datefmt

    def format(self, record):
        """
        Format the log message for our purposes. It will output the date and time in the format: YYYY-MM-DD HH:MM:
        followed by the log level, followed by the log name, followed by the log message.
        If the message is a dictionary, it will convert it to a JSON string and output to the log.
        :param record: Log record.
        :return: Formatted log message.
        """
        # Get the log name
        log_name = record.name.split('.')[-1].rjust(8)

        # Format the timestamp
        timestamp_datetime = datetime.fromtimestamp(record.created)
        formatted_datetime = timestamp_datetime.strftime(self.datefmt)

        # Right-align [%(levelname)s] within a field of width 10
        levelname = f"[{record.levelname}]".ljust(10)

        # Handle the log message
        formatted_message = ': ' + self.format_message(record) if record.msg else ''

        return f"{formatted_datetime} {levelname} [{log_name}]{formatted_message}"

    def format_message(self, record):
        """
        Format the log message based on its type.
        :param record: Log record.
        :return: Formatted log message as a string.
        """
        try:
            if isinstance(record.msg, dict):
                return json.dumps(record.msg, default=self.datetime_handler)

            if isinstance(record.msg, bytes):
                return record.msg.decode('utf-8')

            return str(record.msg % tuple(record.args)) if record.args else str(record.msg)
        except (TypeError, ValueError) as e:
            return f"Error formatting message: {e}"
        except Exception as e:  # pylint:disable=broad-exception-caught
            return f"Unexpected error formatting message: {e}"

    @staticmethod
    def datetime_handler(x):
        """
        Custom handler for serializing datetime objects.
        :param x: Object to serialize.
        :return: ISO formatted datetime string.
        """
        if isinstance(x, (datetime,)):
            return x.isoformat()
        elif isinstance(x, timedelta):
            return str(x)
        raise TypeError(f"Type not serializable: {type(x)}")


def datetime_handler(obj):
    """
    Convert dictionary objects to strings.  Objects that are supported are datetime, date, time, timedelta, Decimal.
    :param obj:
    :return:
    """
    if isinstance(obj, (datetime, date, time)):
        return obj.isoformat()
    if isinstance(obj, timedelta):
        return str(obj)  # Serialize timedelta as a string
    if isinstance(obj, Decimal):
        return str(obj)
    raise TypeError("Type not serializable")


def get_logger(logger_name: Optional[str] = None, log_level: Optional[str] = None):
    """
    Return a logger with the given name and log level.
    If log_level is not specified, it will use the environment variable LOG_LEVEL.

    if there is no environment variable LOG_LEVEL, it will default to INFO.

    :param logger_name:
    :param log_level:
    :return:
    """

    if not log_level:
        log_level = os.getenv('LOG_LEVEL', 'INFO')

    # Turn off propegation on root logger.  Retrn it if no logger name is specified.

    if not logger_name:
        logger = logging.getLogger()
    else:
        logger = logging.getLogger(logger_name)

    if not logger.handlers:

        logger.setLevel(log_level)
        logger.propagate = False

        handler = logging.StreamHandler()
        handler.setFormatter(CustomFormatter())

        logger.addHandler(handler)

    return logger
