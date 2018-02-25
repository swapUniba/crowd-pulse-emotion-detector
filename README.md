# crowd-pulse-emotion-detector
This plugin detects the emotion for all input messages. Emotions possible values:
joy, sad, fear, anger, disgust, surprise.

You can specify the configuration option "calculate" with one of the following values:
- all: to calculate the emotions for all messages coming from the stream;
- new: to calculate the emotions of the messages with no emotions (property is null);

Example of usage:

```json
{
  "process": {
    "name": "emotion-tester",
    "logs": "/opt/crowd-pulse/logs"
  },
  "nodes": {
    "fetch": {
      "plugin": "message-fetch",
      "config": {
        "db": "test"
      }
    },
    "emotionDetector": {
      "plugin": "emotion-detector",
      "config": {
        "calculate": "new"
      }
    },
    "persistance": {
      "plugin": "message-persist",
      "config": {
        "db": "test"
      }
    }
  },
  "edges": {
    "fetch": [
      "emotionDetector"
    ],
    "emotionDetector": [
      "persistance"
    ]
  }
}
```