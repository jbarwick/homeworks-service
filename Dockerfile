# Use the official Python image from the Docker Hub
FROM python:3.11-slim AS base

# Update the image
RUN apt-get update & apt-get upgrade -y

# Set the working directory in the container
WORKDIR /app

# Copy the distribution file into the container
COPY dist/*.whl ./

# Install the distribution file using pip
RUN pip install --no-cache-dir /app/*.whl

RUN rm *.whl

# Copy the rest of the application code into the container
COPY asgi.py asgi.py

# Expose port 9992 to the outside world
EXPOSE 9992

# Command to run the FastAPI application using Uvicorn
CMD ["fastapi", "run", "asgi.py", "--port", "9992"]
