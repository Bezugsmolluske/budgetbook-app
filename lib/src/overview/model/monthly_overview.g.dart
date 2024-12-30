// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'monthly_overview.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MonthlyOverview _$MonthlyOverviewFromJson(Map<String, dynamic> json) =>
    MonthlyOverview(
      (json['month'] as num).toInt(),
      json['expenses'] as String,
      json['incomes'] as String,
      json['sum'] as String,
    );

Map<String, dynamic> _$MonthlyOverviewToJson(MonthlyOverview instance) =>
    <String, dynamic>{
      'month': instance.month,
      'expenses': instance.expenses,
      'incomes': instance.incomes,
      'sum': instance.sum,
    };
