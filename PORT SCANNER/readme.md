# Port Scanner Project 🚀

Welcome to the **Port Scanner** project! This tool helps you scan open ports on a given IP address or hostname.

## Features ✨

- Fast and efficient port scanning ⚡
- Supports both TCP and UDP protocols 🌐
- Customizable scan range and timeout settings ⏱️
- Detailed scan reports with open/closed port status 📊

## Installation 📥

To install the Port Scanner, follow these steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/portscanner.git
   ```
2. Navigate to the project directory:
   ```sh
   cd portscanner
   ```
3. Build the project using Maven:
   ```sh
   mvn clean install
   ```

## Usage 🛠️

To run the Port Scanner, use the following command:

```sh
java -jar target/portscanner-1.0.jar -h <hostname> -p <port-range>
```

Example:

```sh
java -jar target/portscanner-1.0.jar -h example.com -p 1-65535
```

## Contributing 🤝

We welcome contributions! Please read our [contributing guidelines](CONTRIBUTING.md) before submitting a pull request.

## License 📄

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
