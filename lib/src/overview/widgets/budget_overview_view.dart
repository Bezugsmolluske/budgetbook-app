import 'package:budgetbook_app/src/overview/model/budget_overview.dart';
import 'package:budgetbook_app/src/overview/widgets/yearly_overview_view.dart';
import 'package:flutter/material.dart';

class BudgetOverviewView extends StatelessWidget {
  const BudgetOverviewView({
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
          .map((element) => YearlyOverviewView(
                yearlyOverview: element,
              ))
          .toList(),
    );
  }
}
