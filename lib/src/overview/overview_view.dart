import 'package:budgetbook_app/src/overview/api/overview_api.dart';
import 'package:budgetbook_app/src/overview/model/budget_overview.dart';
import 'package:budgetbook_app/src/overview/widgets/budget_overview_view.dart';
import 'package:budgetbook_app/src/settings/settings_view.dart';
import 'package:flutter/material.dart';

class OverviewView extends StatefulWidget {
  const OverviewView({super.key});
  static const routeName = '/overview';

  @override
  State<OverviewView> createState() => _OverviewViewState();
}

class _OverviewViewState extends State<OverviewView> {
  late Future<BudgetOverview> budgetOverview;

  @override
  void initState() {
    super.initState();
    budgetOverview = getBudgetOverview();
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Overview'),
          backgroundColor: Colors.teal,
          actions: [
            IconButton(
              icon: const Icon(Icons.settings),
              onPressed: () {
                Navigator.restorablePushNamed(context, SettingsView.routeName);
              },
            ),
          ],
        ),
        body: FutureBuilder<BudgetOverview>(
          future: budgetOverview,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return BudgetOverviewView(budgetOverview: snapshot.data!);
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
