# Q-Rapids Strategic Dashboard ![](https://img.shields.io/badge/License-Apache2.0-blue.svg) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=q-rapids_qrapids-dashboard&metric=alert_status)](https://sonarcloud.io/dashboard?id=q-rapids_qrapids-dashboard)
A dashboard for visualizing the quality of the company's products. This strategic dashboard is complemented with some specific features to support decision-makers managing **quality requirements**.

This component has been created as a result of the Q-Rapids project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732253.

## Main Functionality
The main functionalities of the current version of Q-Rapids Dashboard are: providing several ways to visualize and explore the available data, generate predictions of the existing assessments, perform simulations on how the strategic indicators will evolve based on the value of the factors and generate quality requirements to correct deviations on the assessments.

## Technologies
|Property|Description|
| -------------------- | --------------------------------|
| Type of component    | Web Application                 |
| Build                | .war                            |
| Programming language | Java                            |
| DBMS                 | PostgreSQL                       |
| Frameworks           | Spring Boot, AngularJS, Gradle  |
| External libraries   | Chart.js, ElasticSearch java API|

## How to build
This is a Gradle project. You can use any IDE that supports Gradle to build it, or alternatively you can use the command line using the Gradle wrapper with the command *__gradlew__* if you don't have Gradle installed on your machine or with the command *__gradle__* if you do, followed by the task *__war__*.

```
# Example: using Gradle wrapper to build with dependencies
cd qrapids-dashboard
gradlew war
```
After the build is done the WAR file can be found at the __build/libs__ directory

## Documentation

You can find the user documentation in the repository [Wiki](https://github.com/q-rapids/qrapids-dashboard/wiki) and the technical documentation of the RESTful API [here](https://q-rapids.github.io/qrapids-dashboard).

## Contributing

You can find guidelines to contribute to the project in the [Q-Rapids repository](https://github.com/q-rapids/q-rapids/blob/master/CONTRIBUTING.md).

## Licensing

Software licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Contact

For problems regarding this component, please open an issue in the [issues section](https://github.com/q-rapids/qrapids-dashboard/issues). You can find some guidelines for submitting issues in the [Q-Rapids repository](https://github.com/q-rapids/q-rapids/blob/master/CONTRIBUTING.md).

For general information, please use the [Q-Rapids project website](http://www.q-rapids.eu/contact).

