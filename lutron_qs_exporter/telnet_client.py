"""
Small telnet client for the QS processor.

The standard library telnetlib module is removed in Python 3.13. This client
covers the subset the exporter uses: connect, write, read until a token, close.
Option negotiations are refused so a server that sends them does not stall.
"""

import socket
import time
from typing import Optional

_IAC = 255
_DONT = 254
_DO = 253
_WONT = 252
_WILL = 251
_SB = 250
_SE = 240


class ProcessorTelnet:
    """One telnet connection. The caller closes it."""

    def __init__(self, host: str, port: int, timeout: float):
        self._timeout = timeout
        self._sock = socket.create_connection((host, port), timeout)
        self._sock.settimeout(timeout)
        self._raw = bytearray()
        self._cooked = bytearray()

    def close(self):
        """Close the socket. A second close is ignored."""
        try:
            self._sock.close()
        except OSError:
            pass

    def write(self, data: bytes):
        """Send bytes. Raises OSError or EOFError if the socket is gone."""
        try:
            self._sock.sendall(data)
        except OSError as exc:
            raise EOFError("connection closed while sending") from exc

    def read_until(self, token: bytes, timeout: Optional[float] = None) -> bytes:
        """
        Return bytes through the first token.

        On timeout, return whatever has arrived and drop it from the buffer.
        The caller decides whether the token was present.
        """
        if timeout is None:
            timeout = self._timeout
        deadline = time.monotonic() + timeout
        while True:
            found = self._cooked.find(token)
            if found != -1:
                end = found + len(token)
                data = bytes(self._cooked[:end])
                del self._cooked[:end]
                return data
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                break
            self._sock.settimeout(remaining)
            try:
                chunk = self._sock.recv(4096)
            except TimeoutError:
                break
            if not chunk:
                raise EOFError("processor closed the connection")
            self._consume(chunk)
        data = bytes(self._cooked)
        self._cooked.clear()
        return data

    def _consume(self, chunk: bytes):
        """Move payload bytes into the cooked buffer and answer telnet options."""
        self._raw.extend(chunk)
        while self._raw:
            if self._raw[0] != _IAC:
                self._cooked.append(self._raw.pop(0))
                continue
            if len(self._raw) < 2:
                return
            command = self._raw[1]
            if command == _IAC:
                self._cooked.append(_IAC)
                del self._raw[:2]
                continue
            if command in (_WILL, _WONT, _DO, _DONT):
                if len(self._raw) < 3:
                    return
                option = self._raw[2]
                del self._raw[:3]
                reply_command = _DONT if command in (_WILL, _WONT) else _WONT
                self.write(bytes((_IAC, reply_command, option)))
                continue
            if command == _SB:
                end = self._raw.find(bytes((_IAC, _SE)))
                if end == -1:
                    return
                del self._raw[: end + 2]
                continue
            del self._raw[:2]
