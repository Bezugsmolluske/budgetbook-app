import 'package:flutter/material.dart';

class IncomesView extends StatefulWidget {
  const IncomesView({super.key});
  static const routeName = '/incomes';

  @override
  State<IncomesView> createState() => _IncomesViewState();
}

class _IncomesViewState extends State<IncomesView> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Einkommen'),
          backgroundColor: Colors.teal,
        ),
        body: Text('TODO'),
      );
    });
  }
}
