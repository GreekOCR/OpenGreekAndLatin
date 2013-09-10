sudo apt-get install openjdk-7-jdk lynx python-dev g++ libtiff-dev python-wxTools libpng12-dev python-numpy python-scipy python-matplotlib python-setuptools liblept3 imagemagick libfreeimage3 libxml2-dev libxslt1-dev python-libxslt1 libsaxonb-java gzip unzip sbt git gridengine-master gridengine-client gridengine-exec
sudo easy_install mahotas lxml nltk
sudo qconf -am $USERNAME
qconf -as $HOSTNAME
qconf -au $USERNAME arusers
qconf -aq $QUEUENAME
wget http://downloads.sourceforge.net/project/gamera/gamera/gamera-3.4.0/gamera-3.4.0.tar.gz && tar -xzf gamera-3.4.0.tar.gz && rm gamera-3.4.0.tar.gz
wget http://gamera.informatik.hsnr.de/addons/ocr4gamera/ocr-1.0.6.tar.gz && tar -xzf ocr-1.0.6.tar.gz && rm ocr-1.0.6.tar.gz
cd gamera-3.4.0 && python setup.py build && sudo python setup.py install && cd ..
cd ocr-1.0.6 && python setup.py build && sudo python setup.py install && cd ..
cd bin/rigaudon/Gamera/greekocr-1.0.0 && python setup.py build && sudo python setup.py install && cd ../../../..
cd bin/hocrinfoaggregator && sbt clean compile assembly && cd ../..
cd bin/cophiproofreader && mvn package && cd ../..
cd db/exist && ./build.sh && cd ../..
echo "deploy proofreader"
echo "start exist db"