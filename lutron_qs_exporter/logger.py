"""
Initialize a logger for my application.

This custom logger is opinionated about the format.  You can't change it but you can re-code it if you want.

The feature here is that if you pass a dict to the logger, it will convert it to a JSON string and output to the log.

Also, the date is output in the format: YYYY-MM-DD HH:MM:SS.

"""
import datetime
import json
import logging
import os
from datetime import datetime, date, time, timedelta
from decimal import Decimal


class CustomFormatter(logging.Formatter):
    """
    Create a custom formatter for the logger.  This formatter will output the date and time in
    the format: YYYY-MM-DD HH:MM:SS.

    It will also convert the log message into JSON if the message is a dictionary.

    """

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    def format(self, record):
        """

        Format the log message for our purposes.  It will output the date and time in the format: YYYY-MM-DD HH:MM:
        followed by the log level, followed by the log name, followed by the log message.

        if the message is a dictionary, it will convert it to a JSON string and output to the log.

        :param record:
        :return:
        """
        # Right-align [%(levelname)s] within a field of width 15
        if record.module == '__init__':
            parts = record.name.rsplit('.', 1)  # Split at the last period
            log_name = parts[-1] if len(parts) > 1 else record.name
        else:
            log_name = record.module

        timestamp_datetime = datetime.fromtimestamp(record.created)
        formatted_datetime = timestamp_datetime.strftime(self.datefmt)
        levelname = f"[{record.levelname}]".rjust(10)

        if record.msg:

            if isinstance(record.msg, dict):
                formatted_message = json.dumps(record.msg, default=datetime_handler)
            elif isinstance(record.msg, bytes):
                formatted_message = record.msg.decode('utf-8')
            else:
                formatted_message = str(record.msg).strip() \
                    if not record.args else str(record.msg % tuple(record.args)).strip()

            return f"{formatted_datetime} {levelname} {log_name}: {formatted_message}"

        return f"{formatted_datetime} {levelname} {log_name}"


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


def get_logger(logger_name, log_level: str = None):
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

    log_level = logging.getLevelName(log_level.upper())

    logger = logging.getLogger(logger_name, )
    logger.setLevel(log_level)
    logger.propagate = False

    handler = logging.StreamHandler()
    handler.setFormatter(CustomFormatter(datefmt='%Y-%m-%d %H:%M:%S'))

    logger.addHandler(handler)

    return logger
