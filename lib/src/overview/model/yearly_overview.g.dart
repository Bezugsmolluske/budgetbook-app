// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'yearly_overview.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

YearlyOverview _$YearlyOverviewFromJson(Map<String, dynamic> json) =>
    YearlyOverview(
      (json['year'] as num).toInt(),
      json['sum'] as String,
      json['expensesSum'] as String,
      json['incomesSum'] as String,
      (json['monthlyOverviews'] as List<dynamic>)
          .map((e) => MonthlyOverview.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$YearlyOverviewToJson(YearlyOverview instance) =>
    <String, dynamic>{
      'year': instance.year,
      'sum': instance.sum,
      'expensesSum': instance.expensesSum,
      'incomesSum': instance.incomesSum,
      'monthlyOverviews':
          instance.monthlyOverviews.map((e) => e.toJson()).toList(),
    };
