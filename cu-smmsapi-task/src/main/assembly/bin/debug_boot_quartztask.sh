#!/bin/sh  

PRG_NAME=cu-quartztask-server
WORK_DIR=$(cd `dirname $0`; pwd)/../
LOG_DIR="$WORK_DIR"/logs
PID_FILE=${LOG_DIR}/${PRG_NAME}.pid

JAVA=java
JAVA_OPTS=" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Xms256m -Xmx1024m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-CMSConcurrentMTEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled -Dwork.dir=${WORK_DIR}"
CLASS_PATH=" -classpath "$(echo ${WORK_DIR}lib/*.jar|sed 's/ /:/g')
CLASS=com.aotain.smmsapi.task.QuartzMain

if [ ! -d "${LOG_DIR}" ]; then
  mkdir -p ${LOG_DIR}
fi
    
cd $WORK_DIR  
  
case "$1" in  
  
  start)
  	if [ -f "${PID_FILE}" ]; then
    	echo "${PRG_NAME} is running,pid=`cat ${PID_FILE}`."
    else
    	exec "$JAVA" $JAVA_OPTS $CLASS_PATH $CLASS>> ${LOG_DIR}/${PRG_NAME}.log 2>&1 &
		echo "${PRG_NAME} is running,pid=$!." 
    	echo $! > ${PID_FILE} 
        echo "${PRG_NAME} start----> "`date  '+%Y-%m-%d %H:%M:%S'` >>${LOG_DIR}/${PRG_NAME}.out
    fi
    ;;  
  
  stop)
  	if [ -f "${PID_FILE}" ]; then
    	kill -15 `cat ${PID_FILE}`
    	for i in {1..10}
    	do
	    	ps -p `cat ${PID_FILE}` > /dev/null
	        if [ $? -eq 0 ]; then
	            echo -ne "\rtring to stop process ${i}s ..." 
	            sleep 1;
	        else 
	        	echo -ne "\n"
	        	break
	        fi
	    done
	    ps -p `cat ${PID_FILE}` > /dev/null
	    if [ $? -eq 0 ]; then
	    	echo -ne "\rtring to kill process ..." 
	    	kill -9 ${PID_FILE}
		fi
		rm -rf ${PID_FILE}	        
    	echo "${PRG_NAME} is stopped."
	echo "${PRG_NAME} end----> "`date  '+%Y-%m-%d %H:%M:%S'` >>${LOG_DIR}/${PRG_NAME}.out
    else
    	echo "${PRG_NAME} is not running."
    fi
    ;; 
  
  restart)  
    bin/$0 stop  
    sleep 1  
    bin/$0 start  
    ;;  

  status)
  	if [ -f "${PID_FILE}" ]; then
    	echo "${PRG_NAME} is running,pid=`cat ${PID_FILE}`."
    else
    	echo "${PRG_NAME} is not running."
    fi
    ;;
    
  *)  
    echo "Usage: ${PRG_NAME}.sh {start|stop|restart|status}"  
    ;;  
  
esac  
  
exit 0 
