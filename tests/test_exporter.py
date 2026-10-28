"""Fixture tests. These do not contact a processor."""

import requests

from lutron_qs_exporter.config import ConfigurationData
from lutron_qs_exporter.database import xml_to_dict
from lutron_qs_exporter.metrics import login, parse_device, parse_output
import lutron_qs_exporter.metrics as metrics

DESIGN_XML = b"""<?xml version="1.0"?>
<Project Name="House">
  <Areas>
    <Area Name="Main Floor" IntegrationID="1">
      <Areas>
        <Area Name="Kitchen" IntegrationID="2">
          <Outputs>
            <Output Name="Pendants" IntegrationID="10" Wattage="120"/>
            <Output Name="Cans" IntegrationID="11" Wattage="80"/>
          </Outputs>
        </Area>
      </Areas>
    </Area>
  </Areas>
</Project>
"""


def _reset_config(monkeypatch):
    ConfigurationData._instance = None
    for name in ("LUTRON_ADDRESS", "LUTRON_PORT", "LUTRON_USERNAME", "LUTRON_PASSWORD", "LOG_LEVEL"):
        monkeypatch.delenv(name, raising=False)


def test_xml_to_dict_reads_areas_and_zones(monkeypatch):
    class Response:
        content = DESIGN_XML

    monkeypatch.setattr(
        "lutron_qs_exporter.database.requests.get",
        lambda *args, **kwargs: Response(),
    )

    design = xml_to_dict("http://processor.example/DbXmlInfo.xml")
    floor = design["Areas"]["Area"]
    kitchen = floor["Areas"]["Area"]
    outputs = kitchen["Outputs"]["Output"]

    assert floor["Name"] == "Main Floor"
    assert kitchen["Name"] == "Kitchen"
    assert [zone["Name"] for zone in outputs] == ["Pendants", "Cans"]
    assert outputs[0]["Wattage"] == 120


def test_xml_to_dict_returns_empty_design_when_the_request_fails(monkeypatch):
    def fail(*args, **kwargs):
        raise requests.exceptions.Timeout()

    monkeypatch.setattr("lutron_qs_exporter.database.requests.get", fail)

    design = xml_to_dict("http://processor.example/DbXmlInfo.xml")

    assert design == {"ProjectName": "Unknown", "Areas": None}


def test_parse_output_reads_zone_level():
    assert parse_output("~OUTPUT,10,1,75.5") == 75.5


def test_parse_output_does_not_invent_a_level():
    assert parse_output("not a reply") == 0.0


def test_parse_device_reads_led_state():
    assert parse_device("~DEVICE,22,5,9,1") == 1.0


def test_parse_device_rejects_a_short_reply():
    assert parse_device("~DEVICE,22,5") == 0.0


def test_missing_environment_variables_return_no_config(monkeypatch):
    _reset_config(monkeypatch)

    assert ConfigurationData.get_instance() == {}


def test_bad_port_returns_no_config(monkeypatch):
    _reset_config(monkeypatch)
    monkeypatch.setenv("LUTRON_ADDRESS", "192.0.2.1")
    monkeypatch.setenv("LUTRON_USERNAME", "default")
    monkeypatch.setenv("LUTRON_PASSWORD", "default")
    monkeypatch.setenv("LUTRON_PORT", "nope")

    assert ConfigurationData.get_instance() == {}


def test_login_failure_returns_none_and_does_not_retry(monkeypatch):
    _reset_config(monkeypatch)
    monkeypatch.setenv("LUTRON_ADDRESS", "192.0.2.1")
    monkeypatch.setenv("LUTRON_PORT", "23")
    monkeypatch.setenv("LUTRON_USERNAME", "default")
    monkeypatch.setenv("LUTRON_PASSWORD", "default")
    ConfigurationData._instance = None

    class Unavailable:
        def __init__(self, *args, **kwargs):
            raise TimeoutError("timed out")

    monkeypatch.setattr(metrics, "ProcessorTelnet", Unavailable)

    assert login() is None
