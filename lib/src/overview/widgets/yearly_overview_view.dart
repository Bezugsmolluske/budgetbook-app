import 'package:budgetbook_app/src/overview/model/yearly_overview.dart';
import 'package:budgetbook_app/src/overview/widgets/yearly_overview_table.dart';
import 'package:flutter/material.dart';

class YearlyOverviewView extends StatelessWidget {
  const YearlyOverviewView({
    super.key,
    required this.yearlyOverview,
  });

  final YearlyOverview yearlyOverview;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            yearlyOverview.year.toString()),
        SizedBox(height: 8),
        YearlyOverviewTable(monthlyOverviews: yearlyOverview.monthlyOverviews),
        SizedBox(height: 16),
      ],
    );
  }
}
