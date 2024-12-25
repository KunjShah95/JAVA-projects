# HTTP Redirector

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Contributions](https://img.shields.io/badge/Contributions-Welcome-brightgreen.svg)

🚀 A simple and efficient HTTP Redirector built with Java.

## Features

- 🌐 **URL Redirection**: Redirect HTTP requests to specified URLs.
- 🔒 **Secure**: Supports HTTPS for secure redirection.
- ⚡ **High Performance**: Optimized for speed and efficiency.
- 📊 **Logging**: Detailed request logging for monitoring and debugging.

## Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/http-redirector.git
   ```
2. Navigate to the project directory:
   ```sh
   cd http-redirector
   ```
3. Compile the project:
   ```sh
   javac -d bin $(find . -name "*.java")
   ```

## Usage

1. Run the server:
   ```sh
   java -cp bin com.yourpackage.Main
   ```
2. Configure your redirections in the `config.yml` file:
   ```yaml
   redirects:
     - from: "/old-path"
       to: "https://newsite.com/new-path"
     - from: "/another-old-path"
       to: "https://anothersite.com/another-new-path"
   ```
3. Start the server:
   ```sh
   java -cp bin com.yourpackage.Main
   ```

## Prerequisites

Ensure you have the following installed:

- Java Development Kit (JDK) 8 or higher
- Git

## Running Tests

To run tests, execute the following command:

```sh
java Redirector http://www.linkedin.com/ portnumebr
```

## Acknowledgements

Special thanks to all contributors and the open-source community.

## Additional Resources

- [Java Documentation](https://docs.oracle.com/en/java/)
- [Markdown Guide](https://www.markdownguide.org/)
- [Shields.io](https://shields.io/)

2. Configure your redirections in the `config.yml` file.

## Configuration

Edit the `config.yml` file to set up your redirections:

```yaml
redirects:
  - from: "/old-path"
    to: "https://newsite.com/new-path"
  - from: "/another-old-path"
    to: "https://anothersite.com/another-new-path"
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

📧 For any inquiries, please contact @KunjShah95 on github

Made with ❤️ by [KunjShah]
