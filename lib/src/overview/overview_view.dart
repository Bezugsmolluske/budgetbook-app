import 'package:flutter/material.dart';

const months = [
  'Januar',
  'Februar',
  'März',
  'April',
  'Mai',
  'Juni',
  'Juli',
  'August',
  'September',
  'Oktober',
  'November',
  'Dezember',
];

class OverviewView extends StatelessWidget {
  const OverviewView({super.key});

  static const routeName = '/overview';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Overview'),
        backgroundColor: Colors.blue,
      ),
      body: ListView(
        children: months.map((month) => OverviewItem(month: month)).toList(),
      ),
    );
  }
}

class OverviewItem extends StatelessWidget {
  const OverviewItem({
    super.key,
    required this.month,
  });

  final String month;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(32, 0, 16, 0),
      child: Text(month, style: TextStyle(fontSize: 18)),
    );
  }
}
