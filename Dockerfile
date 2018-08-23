FROM jboss-datagrid-7/datagrid72-openshift:1.1
COPY modules/datagrid/72/launch/added/launch/infinispan-config.sh /opt/datagrid/bin/launch/infinispan-config.sh
