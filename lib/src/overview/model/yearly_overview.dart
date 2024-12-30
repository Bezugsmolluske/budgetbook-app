import 'package:budgetbook_app/src/overview/model/monthly_overview.dart';
import 'package:json_annotation/json_annotation.dart';

part 'yearly_overview.g.dart';

@JsonSerializable(explicitToJson: true)
class YearlyOverview {
  final int year;
  final List<MonthlyOverview> monthlyOverviews;

  YearlyOverview(this.year, this.monthlyOverviews);

  factory YearlyOverview.fromJson(Map<String, dynamic> json) =>
      _$YearlyOverviewFromJson(json);

  Map<String, dynamic> toJson() => _$YearlyOverviewToJson(this);
}
