mkdir -p /data/kafka/logs;
cd /data/kafka;
tar xzf kafka_2.10-0.8.2.2.tgz;
ln -s kafka_2.10-0.8.2.2 default;
cd /data/kafka/default/config;
vim server.properties;

#58 log.dirs=/data/kafka/logs
#81 log.flush.interval.messages=10000
#84 log.flush.interval.ms=1000
#118 zookeeper.connect=10.6.100.1:2181,10.6.100.2:2181,10.6.100.3:2181/kafka

#20 broker.id=0
#20 broker.id=1
#20 broker.id=2

cd /data/kafka/default;
./bin/kafka-server-start.sh -daemon ./config/server.properties;
