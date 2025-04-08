# FloreonBOT

FloreonBOT is an open-source Telegram Bot built using **Java with Spring**, designed to enhance group chat experiences by providing various utilities such as weather updates, conversation summaries, and AI-driven responses. More features are planned to be added in the future.

## Features

- **Weather Checking**

  - Provides real-time weather information for a specified location.
  - Example Usage: `/weather Bucharest` or `/temperature Bucharest` or `/forecast Bucharest` 
  - Utilizes OpenWeather API and Open-meteo API.

- **Conversation Summarization**

  - Generates a summary of conversations within a specified number of hours.
  - Example Usage: `/summary 3` (Provides a summary of the last 3 hours of conversation in a group chat.)

- **Ask Gemini**

  - Allows users to ask questions and receive AI-generated responses from Gemini.
  - Example Usage: `/Gemini What's the temperature of the Sun?`

- **Ask ChatGPT**

  - Allows users to ask questions and receive AI-generated responses from ChatGPT.
  - Example Usage: `/chatgpt Why are the plants green?`

- **Ask ChatGPT with Web Search**

  - Integrates web search capabilities to provide updated and relevant answers.
  - Example Usage: `/chatgptweb What happened today?`

- **Joke**

  - The bot sends a random joke based on specified categories or topics.
  - Example Usage: 
    - `/joke` (sends a random joke)
    - `/joke programming` (sends a programming-related joke)
    - `/joke dad` (sends a dad joke)

- **Dice**

  - Roll a dice.
  - Example Usage: `/dice`
  
- **Vanish**

  - Delete a specific number of messages from the chat.
  - Example Usage: `/vanish 30`
  
- **Group chat poll**

  - Create a poll.
  - Example Usage: `/What color is the Sun?, yellow, white`

- **Planned Features**

  - Additional functionalities will be added over time.

## Contributing

Contributions are welcome! Feel free to fork, open issues, submit pull requests, or suggest new features.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Telegram Bot API
- OpenAI ChatGPT API
- Gemini API
- OpenWeather API (Free tier)
- Open-meteo API

