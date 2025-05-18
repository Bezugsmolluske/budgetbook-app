// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'incomes.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Incomes _$IncomesFromJson(Map<String, dynamic> json) => Incomes(
      json['sum'] as String,
      (json['incomes'] as List<dynamic>)
          .map((e) => Income.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$IncomesToJson(Incomes instance) => <String, dynamic>{
      'sum': instance.sum,
      'incomes': instance.incomes.map((e) => e.toJson()).toList(),
    };
