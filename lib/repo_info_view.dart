import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class RepoInfoView extends StatelessWidget {
  final String repoName = 'Freecell Flutter';
  final String repoAuthor = 'Jules';
  final String repoUrl = 'https://github.com/jules-org/freecell-flutter';

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.all(16.0),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Repository Information',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 16.0),
            ListTile(
              leading: const Icon(Icons.code),
              title: const Text('Name'),
              subtitle: Text(repoName),
            ),
            ListTile(
              leading: const Icon(Icons.person),
              title: const Text('Author'),
              subtitle: Text(repoAuthor),
            ),
            ListTile(
              leading: const Icon(Icons.link),
              title: const Text('URL'),
              subtitle: Text(
                repoUrl,
                style: const TextStyle(color: Colors.blue),
              ),
              onTap: () async {
                final Uri url = Uri.parse(repoUrl);
                if (!await launchUrl(url)) {
                  throw Exception('Could not launch $url');
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
