FROM rabbitmq:3.13-management

USER root

RUN apt-get update && apt-get install -y wget \
 && wget https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v3.13.0/rabbitmq_delayed_message_exchange-3.13.0.ez -P /opt/rabbitmq/plugins/ \
 && rabbitmq-plugins enable --offline rabbitmq_management rabbitmq_delayed_message_exchange rabbitmq_prometheus
