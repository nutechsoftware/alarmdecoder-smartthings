import socket


class SSDPClient(object):
    MCAST_ADDRESS = '239.255.255.250'
    MCAST_PORT = 1900

    DISCOVER_MESSAGE = ('M-SEARCH * HTTP/1.1\r\n' +
                         'ST: %(library)s:%(service)s\r\n' +
                         'MX: 3\r\n' +
                         'MAN: "ssdp:discover"\r\n' +
                         'HOST: 239.255.255.250:1900\r\n\r\n')

    def __init__(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
        sock.bind(('', 0))

        self._socket = sock

    def discover(self):
        message = self.DISCOVER_MESSAGE % dict(library='alarmdecoder', service='api')

        self._socket.sendto(message, (self.MCAST_ADDRESS, self.MCAST_PORT))

        try:
            data = self._socket.recv(1024)
        except socket.timeout:
            pass
        else:
            print data


def main():
    socket.setdefaulttimeout(5)

    client = SSDPClient()
    client.discover()

if __name__ == '__main__':
    main()

