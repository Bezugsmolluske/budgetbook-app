import 'package:flutter/material.dart';

class ExpensesView extends StatefulWidget {
  const ExpensesView({super.key});
  static const routeName = '/expenses';

  @override
  State<ExpensesView> createState() => _ExpensesViewState();
}

class _ExpensesViewState extends State<ExpensesView> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Ausgaben'),
          backgroundColor: Colors.teal,
        ),
        body: Text('TODO'),
      );
    });
  }
}
