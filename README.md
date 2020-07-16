# kafka-topic-ui

UI for viewing or publishing events on a kafka topics built on OpenJFX11 and Kotlin. All the threading on the application
is backed up by coroutines.

It uses the kafka client version 2.2.0.

# Run the UI

- Install JDK11
  
  Required because JavaFx is not distributed evenly across operating systems on JDK8.

- `gradlew run`

## Views

### Topic list view

Lists available topics on the broker and provides a preview window with the first records keys.

The user can then chose a topic, the deserializer to use and open a topic detail view.

### Topic detail view

Subscribes to a topic with read/write capabilities.

Reads can start at the beginning of the topic or at the last registered commit for the consumer. The user can pause
and resume polls.

Writes are done by reading files on the machine for key and value.

## Features to come

- SSL login
- Create/Delete topics
- Finish custom deserializers
- Save preferences to a file
    - User defined application id