import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io';

import 'package:share_extend/share_extend.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    _copyAssets();
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          child: new Center(
            child: new Column(
              children: <Widget>[
                new RaisedButton(
                  onPressed: () {
                    ShareExtend.share("share text", "text");
                  },
                  child: new Text("share text"),
                ),
                new RaisedButton(
                  onPressed: () async {
                    File f = await ImagePicker.pickImage(
                        source: ImageSource.gallery);
                    ShareExtend.share(f.path, "image");
                  },
                  child: new Text("share image"),
                ),
                new RaisedButton(
                  onPressed: () async {
                    File f = await ImagePicker.pickVideo(
                        source: ImageSource.gallery);
                    ShareExtend.share(f.path, "video");
                  },
                  child: new Text("share video"),
                ),
                new RaisedButton(
                  onPressed: () {
                    _shareApplicationDocumentsFile();
                  },
                  child: new Text("share file"),
                ),
                RaisedButton(
                  onPressed: () {
                    _shareMultiple();
                  },
                  child: Text('Share Multiple'),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }

  _copyAssets() async {
    final Directory d = await getApplicationDocumentsDirectory();
    rootBundle.load('assets/videos/s.mp4').then((content) {
      File newFile = File('${d.path}/s.mp4');
      newFile.writeAsBytesSync(content.buffer.asUint8List());
    });

    rootBundle.load('assets/videos/s1.mp4').then((content) {
      File newFile = File('${d.path}/s1.mp4');
      newFile.writeAsBytesSync(content.buffer.asUint8List());
    });
  }

  ///share the documents file
  _shareApplicationDocumentsFile() async {
    Directory dir = await getApplicationDocumentsDirectory();
    File testFile = new File("${dir.path}/flutter/test.txt");
    if (!await testFile.exists()) {
      await testFile.create(recursive: true);
      testFile.writeAsStringSync("test for share documents file");
    }
    ShareExtend.share(testFile.path, "file");
  }

  _shareMultiple() async {
    Directory dir = await getApplicationDocumentsDirectory();
    File f1 = new File("${dir.path}/s1.mp4");
    File f2 = new File("${dir.path}/s.mp4");
    ShareExtend.shareMultiple(filePaths: [f1.path, f2.path]);
  }
}
