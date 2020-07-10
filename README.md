# kafka-topic-ui

UI for viewing or publishing events on a kafka topics.

It uses the kafka client version 2.2.0.

# Run the UI

- Install JDK11
  
  Required because JavaFx is not distributed evenly across operating systems on JDK8.

- `gradlew run`

## Features to come

- Create/Delete topics
- Follow records on detail view
- Finish custom deserializers
- Save preferences to a file