// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'budget_overview.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BudgetOverview _$BudgetOverviewFromJson(Map<String, dynamic> json) =>
    BudgetOverview(
      (json['yearlyOverview'] as List<dynamic>)
          .map((e) => YearlyOverview.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$BudgetOverviewToJson(BudgetOverview instance) =>
    <String, dynamic>{
      'yearlyOverview': instance.yearlyOverview.map((e) => e.toJson()).toList(),
    };
