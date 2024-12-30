import 'package:budgetbook_app/src/overview/api/overview_api.dart';
import 'package:budgetbook_app/src/overview/model/budget_overview.dart';
import 'package:budgetbook_app/src/overview/model/monthly_overview.dart';
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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Overview'),
        backgroundColor: Colors.blue,
      ),
      body: FutureBuilder<BudgetOverview>(
        future: budgetOverview,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return BudgetOverviewTables(budgetOverview: snapshot.data!);
          }
          if (snapshot.hasError) {
            return Text('${snapshot.error}');
          }
          return const CircularProgressIndicator();
        },
      ),
    );
  }
}

class BudgetOverviewTables extends StatelessWidget {
  const BudgetOverviewTables({
    super.key,
    required this.budgetOverview,
  });

  final BudgetOverview budgetOverview;

  @override
  Widget build(BuildContext context) {
    return ListView(
        scrollDirection: Axis.vertical,
        physics: BouncingScrollPhysics(),
        padding: EdgeInsets.all(16),
        children: budgetOverview.yearlyOverview
            .map((element) => Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                        style: TextStyle(
                            fontSize: 24, fontWeight: FontWeight.bold),
                        element.year.toString()),
                    SizedBox(height: 8),
                    YearlyOverviewTable(
                        monthlyOverviews: element.monthlyOverviews),
                    SizedBox(height: 16),
                  ],
                ))
            .toList());
  }
}

class YearlyOverviewTable extends StatelessWidget {
  const YearlyOverviewTable({
    super.key,
    required this.monthlyOverviews,
  });

  final List<MonthlyOverview> monthlyOverviews;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        color: Colors.white,
        boxShadow: [
          BoxShadow(color: Colors.white, spreadRadius: 1),
        ],
      ),
      clipBehavior: Clip.hardEdge,
      child: Table(
          border: TableBorder.all(color: Colors.white),
          columnWidths: {
            0: FlexColumnWidth(),
            1: FlexColumnWidth(),
            2: FlexColumnWidth(),
            3: FlexColumnWidth(),
          },
          defaultVerticalAlignment: TableCellVerticalAlignment.middle,
          children: [
            TableRow(
              decoration: const BoxDecoration(
                color: Colors.deepOrangeAccent,
              ),
              children: [
                CustomTableCell(text: 'Monat'),
                CustomTableCell(
                    text: 'Ausgaben', alignment: Alignment.centerRight),
                CustomTableCell(
                    text: 'Einnahmen', alignment: Alignment.centerRight),
                CustomTableCell(
                    text: 'Gesamt', alignment: Alignment.centerRight),
              ],
            ),
            ...monthlyOverviews.map((monthlyOverview) => TableRow(
                  decoration: const BoxDecoration(
                    color: Color.fromRGBO(158, 158, 158, 1),
                  ),
                  children: [
                    CustomTableCell(text: months[monthlyOverview.month - 1]),
                    CustomTableCell(
                        text: monthlyOverview.expenses,
                        alignment: Alignment.centerRight),
                    CustomTableCell(
                        text: monthlyOverview.incomes,
                        alignment: Alignment.centerRight),
                    CustomTableCell(
                        text: monthlyOverview.sum,
                        alignment: Alignment.centerRight),
                  ],
                ))
          ]),
    );
  }
}

class CustomTableCell extends StatelessWidget {
  const CustomTableCell(
      {super.key, required this.text, this.alignment = Alignment.centerLeft});

  final String text;
  final Alignment alignment;

  @override
  Widget build(BuildContext context) {
    return TableCell(
      verticalAlignment: TableCellVerticalAlignment.middle,
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(alignment: alignment, child: Text(text)),
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
