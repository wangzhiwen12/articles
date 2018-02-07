mkdir -p /data/zookeeper/logs;
mkdir -p /data/zookeeper/data;
cd /data/zookeeper;
tar xzf zookeeper-3.4.9.tar.gz;
ln -s zookeeper-3.4.9 default;
cd /data/zookeeper/default/conf;

echo "" > zoo.cfg;
echo "tickTime=2000" >> zoo.cfg;
echo "initLimit=10" >> zoo.cfg;
echo "syncLimit=5" >> zoo.cfg;
echo "dataDir=/data/zookeeper/data" >> zoo.cfg;
echo "dataLogDir=/data/zookeeper/logs" >> zoo.cfg;
echo "clientPort=2181" >> zoo.cfg;
echo "autopurge.snapRetainCount=60" >> zoo.cfg;
echo "autopurge.purgeInterval=24" >> zoo.cfg;
echo "" >> zoo.cfg;
echo "server.1=s100_18:2888:3888" >> zoo.cfg;
echo "server.2=s100_19:2888:3888" >> zoo.cfg;
echo "server.3=s100_20:2888:3888" >> zoo.cfg;
echo "server.4=s100_21:2888:3888" >> zoo.cfg;
echo "server.5=s100_22:2888:3888" >> zoo.cfg;

echo "1" > /data/zookeeper/data/myid;
echo "2" > /data/zookeeper/data/myid;
echo "3" > /data/zookeeper/data/myid;
echo "4" > /data/zookeeper/data/myid;
echo "5" > /data/zookeeper/data/myid;

cd /data/zookeeper/logs;
/data/zookeeper/default/bin/zkServer.sh start;
