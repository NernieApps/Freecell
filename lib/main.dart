import 'package:flutter/material.dart';
import 'package:freecell/repo_info_view.dart';

void main() => runApp(FreecellApp());

class FreecellApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Freecell',
      home: Scaffold(
        appBar: AppBar(title: Text('Freecell')),
        body: Center(child: RepoInfoView()),
      ),
    );
  }
}
