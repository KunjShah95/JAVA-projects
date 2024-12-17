# Ping Utility

## Overview
This project is a simple Java-based Ping Utility that allows users to check the reachability of a host on an IP network. It sends ICMP Echo Request packets to the target host and listens for Echo Response replies.

## Features
- Send ICMP Echo Request packets
- Display response time for each packet
- Calculate packet loss percentage
- Display minimum, maximum, and average response times

## Requirements
- Java Development Kit (JDK) 8 or higher

## Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/ping-utility.git
    ```
2. Navigate to the project directory:
    ```sh
    cd ping-utility
    ```
3. Compile the project:
    ```sh
    javac -d bin src/*.java
    ```

## Usage
1. Run the Ping Utility:
    ```sh
    java -cp bin com.example.PingUtility <hostname>
    ```
2. Replace `<hostname>` with the target host's domain name or IP address.

## Example
```sh
java -cp bin com.example.PingUtility google.com
```

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.


