# Use an official Python runtime as a parent image
FROM python:3.9-slim

# Set the working directory in the container to /app
WORKDIR /app

# Add the current directory contents into the container at /app
ADD . /app

# Install any needed packages specified in requirements.txt
RUN pip install -q --no-cache-dir -r requirements.txt

# Make port 80 available to the world outside this container
EXPOSE 9992

# Define environment variable
ENV NAME FastAPIApp

# Run main.py when the container launches
CMD ["hypercorn", "asgi:app", "--bind", "0.0.0.0:9992"]
