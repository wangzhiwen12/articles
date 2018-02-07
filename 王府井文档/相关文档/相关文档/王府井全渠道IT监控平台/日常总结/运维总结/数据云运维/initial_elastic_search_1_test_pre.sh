adduser es;
chown -R es.es /data;
su - es;
mkdir -p /data/es/data;
mkdir -p /data/es/logs;
cd /data/es;
tar xzf elasticsearch-2.4.1.tar.gz;
ln -s elasticsearch-2.4.1 default;
cd /data/es/default/config;

echo "cluster.name: wfj-es-test" > elasticsearch.yml;
echo "node.name: node-10" >> elasticsearch.yml;
echo "network.host: "10.6.100.10"" >> elasticsearch.yml;
echo "# node.rack: r1" >> elasticsearch.yml;
echo "# bootstrap.memory_lock: true" >> elasticsearch.yml;
echo "path.data: /data/es/data" >> elasticsearch.yml;
echo "path.logs: /data/es/logs" >> elasticsearch.yml;
echo "http.port: 9200" >> elasticsearch.yml;
echo "transport.tcp.port: 9300" >> elasticsearch.yml;
echo "discovery.zen.ping.unicast.hosts: ["10.6.100.10", "10.6.100.11", "10.6.100.12"]" >> elasticsearch.yml;
echo "discovery.zen.minimum_master_nodes: 1" >> elasticsearch.yml;
echo "gateway.recover_after_nodes: 1" >> elasticsearch.yml;
echo "# node.max_local_storage_nodes: 1" >> elasticsearch.yml;
echo "# action.destructive_requires_name: true" >> elasticsearch.yml;
echo "script.engine.groovy.file.aggs: true" >> elasticsearch.yml;
echo "script.engine.groovy.file.mapping: true" >> elasticsearch.yml;
echo "script.engine.groovy.file.search: true" >> elasticsearch.yml;
echo "script.engine.groovy.file.update: true" >> elasticsearch.yml;
echo "script.engine.groovy.file.plugin: true" >> elasticsearch.yml;
echo "script.engine.groovy.indexed.aggs: true" >> elasticsearch.yml;
echo "script.engine.groovy.indexed.mapping: false" >> elasticsearch.yml;
echo "script.engine.groovy.indexed.search: true" >> elasticsearch.yml;
echo "script.engine.groovy.indexed.update: false" >> elasticsearch.yml;
echo "script.engine.groovy.indexed.plugin: false" >> elasticsearch.yml;
echo "script.engine.groovy.inline.aggs: true" >> elasticsearch.yml;
echo "script.engine.groovy.inline.mapping: false" >> elasticsearch.yml;
echo "script.engine.groovy.inline.search: true" >> elasticsearch.yml;
echo "script.engine.groovy.inline.update: true" >> elasticsearch.yml;
echo "script.engine.groovy.inline.plugin: false" >> elasticsearch.yml;

cd /data/es/default;
./bin/plugin install elasticsearch-inquisitor;
./bin/plugin install mobz/elasticsearch-head;
./bin/plugin install lmenezes/elasticsearch-kopf;

cd /data/es/default/bin;
vim elasticsearch.in.sh;

ES_HEAP_SIZE=8g
ES_HEAP_NEWSIZE=4g

cd /data/es/default;
./bin/elasticsearch -d -p data/es.pid
