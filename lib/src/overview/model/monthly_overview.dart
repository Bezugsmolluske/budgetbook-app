import 'package:json_annotation/json_annotation.dart';

part 'monthly_overview.g.dart';

@JsonSerializable(explicitToJson: true)
class MonthlyOverview {
  int month;
  String expenses;
  String incomes;
  String sum;

  MonthlyOverview(this.month, this.expenses, this.incomes, this.sum);

  factory MonthlyOverview.fromJson(Map<String, dynamic> json) =>
      _$MonthlyOverviewFromJson(json);

  Map<String, dynamic> toJson() => _$MonthlyOverviewToJson(this);
}
