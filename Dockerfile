# Image tag is the version field in pyproject.toml. pip install records that version
# in the package metadata, which the app reads at startup.
FROM python:3.11-slim

RUN apt-get update \
    && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pyproject.toml README.md ./
COPY lutron_qs_exporter ./lutron_qs_exporter
COPY asgi.py .

RUN pip install --no-cache-dir . \
    && rm -rf lutron_qs_exporter build *.egg-info

EXPOSE 9992

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:9992/health')"

# One worker process. Gauges live in that process, and each scrape opens one telnet
# session. A second worker would split the metrics and dial the processor twice.
# No --reload: this is the production server.
CMD ["uvicorn", "asgi:app", "--host", "0.0.0.0", "--port", "9992", "--workers", "1"]
