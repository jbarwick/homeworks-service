from unittest import TestCase
import asyncio

from lutron_qs_exporter import metrics


class TestDatabase(TestCase):

    def test_xml_to_dict(self):

        from lutron_qs_exporter.database import xml_to_dict

        # Example usage
        xml_url = " http://192.168.1.40/DbXmlInfo.xml"
        result_dict = xml_to_dict(xml_url)
        floor1 = result_dict['Areas']['Area']
        # print(result_dict)
        for area in floor1['Areas']['Area']:
            print(area['Name'])
            print(area)

    def test_refresh_metrics(self):
        try:

            from lutron_qs_exporter import refresh_metrics

            result = asyncio.run(metrics())
            self.assertTrue(result)

        except Exception as e:
            self.fail(e)
