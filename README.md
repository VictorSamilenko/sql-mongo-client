# sql-mongo-client

[![Build Status](https://travis-ci.org/VictorSamilenko/sql-mongo-client.svg?branch=master)](https://travis-ci.org/VictorSamilenko/sql-mongo-client)

Для запуска необходимы:
 - jdk8
 - Maven
 - MongoDB

Для инициализации базы `test` необходимо выполнить `/initdb.sh`, также будет инициализирована сущность `client`, к которой, в последующем, можно составлять запросы.

Для настройки подключения к базе используйте `/src/main/resources/mongo.properties`

Для сборки проекта, необходимо выполнить `mvn package -Dmaven.test.skip=true`

Для выполнения тестов, необходимо выполнить `mvn test`

Для запуска проекта, необходимо выполнить `mvn exec:java`

Вам будет предложено ввести ваш SQL запрос, окончив его точкой с запятой(`;`).
После вывода результата, вы сможете повторно вводить ваши запросы.
Для заверщения работы приложения введите `exit`.
