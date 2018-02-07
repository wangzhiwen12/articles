mkdir -p /data/storm/data;
cd /data/storm;
tar xzf apache-storm-0.10.0.tar.gz;
ln -s apache-storm-0.10.0 default;
cd /data/storm/default/conf;

echo "storm.zookeeper.servers:" > storm.yaml;
echo "    - "10.6.100.1"" >> storm.yaml;
echo "    - "10.6.100.2"" >> storm.yaml;
echo "    - "10.6.100.3"" >> storm.yaml;
echo "nimbus.host: "10.6.100.7"" >> storm.yaml;
echo "storm.local.dir: "/data/storm/data"" >> storm.yaml;
echo "ui.port: 18080" >> storm.yaml;
echo "supervisor.slots.ports:" >> storm.yaml;
echo "    - 6700" >> storm.yaml;
echo "    - 6701" >> storm.yaml;
echo "    - 6702" >> storm.yaml;
echo "    - 6703" >> storm.yaml;
echo "    - 6704" >> storm.yaml;
echo "    - 6705" >> storm.yaml;
echo "    - 6706" >> storm.yaml;
echo "    - 6707" >> storm.yaml;
echo "    - 6708" >> storm.yaml;
echo "    - 6709" >> storm.yaml;
echo "    - 6710" >> storm.yaml;
echo "    - 6711" >> storm.yaml;

cd /data/storm/default/;
nohup ./bin/storm supervisor >> /dev/null 2>&1 &
nohup ./bin/storm logviewer >> /dev/null 2>&1 &
nohup ./bin/storm nimbus >> /dev/null 2>&1 &
nohup ./bin/storm ui >> /dev/null 2>&1 &
