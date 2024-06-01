# Use the official Python image from the Docker Hub
FROM python:3.11-slim

# Set the working directory in the container
WORKDIR /app

# Copy the distribution file into the container
COPY dist/*.whl /app/

# Install the distribution file using pip
RUN pip install --no-cache-dir /app/*.whl

# Copy the rest of the application code into the container
COPY . .

# Expose port 9992 to the outside world
EXPOSE 9992

# Command to run the FastAPI application using Uvicorn
CMD ["uvicorn", "asgi:app", "--host", "0.0.0.0", "--port", "9992"]
