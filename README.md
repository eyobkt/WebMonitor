# WebMonitor

WebMonitor is a RESTful web service that allows users to track when a web page has changed.

## How It Works
Users make a `POST` request with a URL and their email to `http://ec2-3-16-44-62.us-east-2.compute.amazonaws.com/api/v1/monitor`. 
The page at the provided URL is checked once an hour for changes to its text. Whenever a change is detected, 
a notification is sent to the provided email.

## Request

### Parameters
Each request must include two parameters in the query string: `url` and `email`.

The value of `url` must satisfy the following conditions:

- It is a full URL, including a protocol, and any necessary query string parameters
- Its protocol is HTTP or HTTPS
- Access requires no authentication other than an API key in the query string

The general form of a request URL is:

    http://ec2-3-16-44-62.us-east-2.compute.amazonaws.com/api/v1/monitor?url=YOUR_URL&email=YOUR_EMAIL

### Methods
Two request methods are supported: `POST` and `DELETE`. A `POST` request creates a Monitor for the page at the provided URL.

## Response
If a `POST` request is successful, a `201 Created` HTTP status code is returned. If a `DELETE` 
request is successful, a `204 No Content` status code is returned. If there is an error, the 
response is a status code in the 300-599 range and possible message of MIME type `text/plain` in the body further 
specifying the error.
