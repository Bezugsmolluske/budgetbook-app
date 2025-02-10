import 'package:budgetbook_app/src/incomes/api/incomes_api.dart';
import 'package:budgetbook_app/src/incomes/incomes_overview_view.dart';
import 'package:budgetbook_app/src/incomes/model/income.dart';
import 'package:flutter/material.dart';

class IncomesView extends StatefulWidget {
  const IncomesView({super.key});
  static const routeName = '/incomes';

  @override
  State<IncomesView> createState() => _IncomesViewState();
}

class _IncomesViewState extends State<IncomesView> {
  late Future<List<Income>> incomes;
  @override
  void initState() {
    super.initState();
    incomes = getIncomes();
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Einkommen'),
          backgroundColor: Colors.teal,
        ),
        body: FutureBuilder<List<Income>>(
          future: incomes,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return IncomesOverviewView(incomes: snapshot.data!);
            }
            if (snapshot.hasError) {
              return Center(child: Text('${snapshot.error}'));
            }
            return Center(child: const CircularProgressIndicator());
          },
        ),
      );
    });
  }
}
