with open("/var/lib/tomcat9/webapps/demo1-1.0-SNAPSHOT/ts_log.log",'r') as f:
    data = [float(line.rstrip()) for line in f]
    print("Average time elapsed for search servlet:")
    print((sum(data)/len(data))/1000000)

with open("/var/lib/tomcat9/webapps/demo1-1.0-SNAPSHOT/tj_log.log",'r') as f:
    data = [float(line.rstrip()) for line in f]
    print("Average time elapsed for JDBC:")
    print((sum(data)/len(data))/1000000)

