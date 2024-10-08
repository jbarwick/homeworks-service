"""
Provides a fastapi application to serve the Prometheus metrics for the Lutron QS processor.
"""
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from prometheus_client import CollectorRegistry, generate_latest
from starlette.responses import Response, RedirectResponse
from ._version import __version__
from .logger import get_logger
from .metrics import (
    refresh_metrics,
    get_design_data,
    information_panel,
    lutron_zone_level,
    lutron_zone_watts,
    lutron_device_leds,
)
from .config import get_host_data


@asynccontextmanager
async def lifespan(application: FastAPI):
    """Do things before and after the app is run"""
    application.state.custom_data = "Some custom data"

    application.state.host_data = get_host_data()

    logger = get_logger(__name__, application.state.host_data.get("log_level", "INFO"))

    logger.warning("Starting FastAPI application...")
    logger.warning("Lutron QS Exporter v%s", __version__)
    logger.warning("Lutron QS Server Address: {address}".format(**application.state.host_data))

    yield

    logger.warning("FastAPI application is shutting down")


app = FastAPI(lifespan=lifespan, version=__version__)


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
    logger = get_logger(__name__)

    logger.warn("Generated metrics...")
    logger.warning("Lutron QS Exporter v%s", __version__)

    await refresh_metrics()

    registry = CollectorRegistry()

    registry.register(information_panel)
    registry.register(lutron_zone_level)
    registry.register(lutron_zone_watts)
    registry.register(lutron_device_leds)

    metrics_data = generate_latest(registry)

    if logger.isEnabledFor(logging.DEBUG):
        logger.debug(metrics_data.decode("utf-8"))

    logger.warning("Process complete.")
    return Response(content=metrics_data, media_type="text/plain")


@app.get("/data")
def get_data():
    """
    This is a little helper function to simply return the design data that was retrieved from the Lutron host.

    :return:
    """
    logger = get_logger(__name__)

    logger.warning("Requested design data")

    design_data = get_design_data()

    logger.debug(design_data)

    return design_data


@app.get("/config")
def get_config():
    """
    A simple endpoint to return the host configuration data.

    :return:
    """
    logger = get_logger(__name__)

    logger.warning("Requested host configuration data")
    logger.warning("Lutron QS Exporter v%s", __version__)

    host_data = get_host_data()
    host_data["version"] = __version__

    logger.debug(host_data)

    return host_data
