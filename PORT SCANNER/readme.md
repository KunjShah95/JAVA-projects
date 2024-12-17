# Port Scanner

This project is a simple port scanner written in Java. It allows you to scan a range of ports on a specified host to determine which ports are open.

## Features

- Scan a range of ports on a specified host
- Display open ports
- Configurable timeout for port scanning

## Requirements

- Java Development Kit (JDK) 8 or higher

## Usage

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/port-scanner.git
   ```
2. Navigate to the project directory:
   ```sh
   cd port-scanner
   ```
3. Compile the Java code:
   ```sh
   javac PortScanner.java
   ```
4. Run the port scanner:
   ```sh
   java PortScanner <host> <startPort> <endPort> <timeout>
   ```
   - `<host>`: The hostname or IP address of the target.
   - `<startPort>`: The starting port number.
   - `<endPort>`: The ending port number.
   - `<timeout>`: The timeout value in milliseconds.

## Example

To scan ports 80 to 100 on `example.com` with a timeout of 200 milliseconds:

```sh
java PortScanner example.com 80 100 200
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any changes.
