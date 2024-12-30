import 'package:budgetbook_app/src/overview/model/yearly_overview.dart';
import 'package:json_annotation/json_annotation.dart';

part 'budget_overview.g.dart';

@JsonSerializable(explicitToJson: true)
class BudgetOverview {
  final List<YearlyOverview> yearlyOverview;

  BudgetOverview(this.yearlyOverview);

  factory BudgetOverview.fromJson(Map<String, dynamic> json) =>
      _$BudgetOverviewFromJson(json);

  Map<String, dynamic> toJson() => _$BudgetOverviewToJson(this);
}
